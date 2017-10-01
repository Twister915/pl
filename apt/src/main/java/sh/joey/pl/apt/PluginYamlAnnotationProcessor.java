/*
 * Copyright (c) 2017 Joseph Sacchini
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the 2nd version of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sh.joey.pl.apt;

import org.yaml.snakeyaml.Yaml;
import sh.joey.pl.Cmd;
import sh.joey.pl.Pl;
import sh.joey.pl.util.YamlUtil;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({
    "sh.joey.pl.Pl",
    "sh.joey.pl.Dep",
    "sh.joey.pl.Cmd"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class PluginYamlAnnotationProcessor extends AbstractProcessor {
    private static final Yaml yaml = YamlUtil.newYaml();

    private Messager messager;
    private ProcessingEnvironment processingEnv;
    private TypeMirror commandType, vanillaCommandType, pluginType;
    private boolean processed;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        this.processingEnv = processingEnv;

        commandType = getTypeByClass("sh.joey.pl.command.JCmd");
        vanillaCommandType = getTypeByClass("org.bukkit.command.CommandExecutor");
        pluginType = getTypeByClass("sh.joey.pl.JPl");
    }

    private TypeMirror getTypeByClass(String name) {
        return processingEnv.getElementUtils().getTypeElement(name).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!processed) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Did not find any annotated classes to generate a plugin.yml for...");
            }

            return false;
        }

        if (!containsAll(annotations, Cmd.class, Pl.class)) {
            return false;
        }

        try {
            for (Element pluginElement : roundEnv.getElementsAnnotatedWith(Pl.class)) {
                if (pluginElement.getKind() != ElementKind.CLASS)
                    throw new ProcessingException(pluginElement, "cannot process non-class");

                TypeElement typeElement = (TypeElement) pluginElement;
                if (!processingEnv.getTypeUtils().isSubtype(typeElement.asType(), pluginType))
                    throw new ProcessingException(pluginElement, "plugin is not a plugin type");

                String name = typeElement.getQualifiedName().toString();
                Pl annotation = typeElement.getAnnotation(Pl.class);

                Map<String, Command> commands = new HashMap<>();

                for (Element commandElement : roundEnv.getElementsAnnotatedWith(Cmd.class)) {
                    if (commandElement.getKind() != ElementKind.CLASS)
                        throw new ProcessingException(commandElement, "You cannot annotate %s (%s) with @Cmd.",
                                                      commandElement.toString(), commandElement.getKind().toString());

                    Cmd cmd = commandElement.getAnnotation(Cmd.class);
                    if (cmd.disable())
                        continue;

                    String commandName = cmd.value().trim();
                    if (commandName.length() == 0)
                        throw new ProcessingException(commandElement, "invalid command name '%s'", commandName);

                    if (!isVanillaCommandHandler(commandElement.asType()) && !processingEnv.getTypeUtils().isAssignable(commandElement.asType(), commandType))
                        throw new ProcessingException(commandElement, "The class '%s' was annotated with @Cmd, but does not implement '%s'!",
                                                      commandElement.toString(), commandType.toString());

                    commands.put(commandName, new Command(commandElement.toString(), commandName, cmd));
                }

                FileObject pluginResource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");
                PluginSpec spec = new PluginSpec(name, annotation, commands);
                try (Writer writer = pluginResource.openWriter()) {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                        bufferedWriter.write(PluginYamlProducer.generate(yaml, spec));
                        bufferedWriter.flush();
                    }
                }

                if (commands.size() > 0) {
                    FileObject commandsResource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/.pl.commands.yml");
                    try (Writer writer = commandsResource.openWriter()) {
                        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                            bufferedWriter.write(CommandMapProducer.commandMapProducer(yaml, commands));
                            bufferedWriter.flush();
                        }
                    }
                }

                messager.printMessage(Diagnostic.Kind.NOTE, "wrote plugin.yml for " + PluginYamlProducer.getName(spec));
                processed = true;
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (Throwable t) {
            error(null, t.getMessage());
            t.printStackTrace();
        }

        return true;
    }

    private boolean isVanillaCommandHandler(TypeMirror handler) {
        return processingEnv.getTypeUtils().isSubtype(vanillaCommandType, handler);
    }

    private static boolean containsAll(Collection<? extends TypeElement> elements, Class<?>... clazzes) {
        if (elements.isEmpty())
            return false;

        if (clazzes.length == 0)
            return false;

        if (clazzes.length == 1) {
            Class<?> type = clazzes[0];
            return elements.stream().anyMatch(v -> v.getQualifiedName().contentEquals(type.getName()));
        }

        Class<?>[] remaining = new Class<?>[clazzes.length - 1];
        System.arraycopy(clazzes, 1, remaining, 0, remaining.length);
        return containsAll(elements, clazzes[0]) && containsAll(elements, remaining);
    }

    public void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
