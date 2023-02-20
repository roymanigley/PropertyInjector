package ch.bytecrowd.propertyinjector;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("ch.bytecrowd.processor.InjectedProperty")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class InjectedPropertyProcessor extends AbstractProcessor {

    class Dependency {
        final String instantiationCommand;

        Dependency(String instantiationCommand) {
            this.instantiationCommand = instantiationCommand;
        }
    }

    class Model {
        final String simpleClassName;
        final String packageName;
        List<Dependency> dependencies = new ArrayList<>();

        Model(String simpleClassName, String packageName) {
            this.simpleClassName = simpleClassName;
            this.packageName = packageName;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Model> map = new HashMap<>();
        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation).forEach(element -> {
                Element classElement = putModelToMapAndReturnKey(map, element);
                addDependenciesToMappedModel(map, element, classElement);
            });
        }
        writeModelsToFiles(map);
        writeInjector(map);
        return true;
    }

    private Element putModelToMapAndReturnKey(Map<String, Model> map, Element element) {
        Element classElement = element.getEnclosingElement().getEnclosingElement();
        String factoryQualifiedName = classElement + "Factory";
        String packageName = factoryQualifiedName.replaceAll("\\.[A-Za-z\\d]+$", "");

        if (!map.containsKey(classElement + "")) {
            map.put(
                    classElement + "",
                    new Model(
                            classElement.getSimpleName().toString(),
                            packageName
                    )
            );
        }
        return classElement;
    }

    private void addDependenciesToMappedModel(Map<String, Model> map, Element element, Element classElement) {
        String dependencyInstantiation = element.getAnnotation(InjectedProperty.class).dependencyInstantiation();
        boolean isSingleton = element.getAnnotation(InjectedProperty.class).singleton();
        if (!dependencyInstantiation.isBlank()) {
            map.get(classElement + "").dependencies.add(
                    new Dependency(dependencyInstantiation)
            );
        } else {
            Dependency dependency;
            if (isSingleton) {
                dependency = new Dependency(element.asType().toString() + "Factory.createSingleton()");
            } else {
                dependency = new Dependency(element.asType().toString() + "Factory.create()");
            }
            map.get(classElement + "").dependencies.add(dependency);
        }
    }

    private void writeModelsToFiles(Map<String, Model> map) {
        map.forEach((className, model) -> {
            try {
                JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(className + "Factory");
                Writer writer = javaFileObject.openWriter();

                String dependencies = model.dependencies.stream()
                        .map(dependency -> dependency.instantiationCommand)
                        .collect(Collectors.joining(",\n          "));

                writer.write("package " + model.packageName + ";\n\n");
                writer.write("\n");
                writer.write("public class " + model.simpleClassName + "Factory {\n");
                writer.write("\n");
                writer.write("  public static " + model.simpleClassName + " create() {\n");
                writer.write("      return new " + model.simpleClassName + "(\n");
                writer.write("          " + dependencies + "\n");
                writer.write("      );\n");
                writer.write("  }\n");
                writer.write("\n");
                writer.write("  private static " + model.simpleClassName + " instance;\n");
                writer.write("\n");
                writer.write("  public static " + model.simpleClassName + " createSingleton() {\n");
                writer.write("      if (instance == null) {\n");
                writer.write("          instance = create();\n");
                writer.write("      }\n");
                writer.write("      return instance;\n");
                writer.write("  }\n");

                writer.write("}");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(
                        "writeModelToFiles failed for: " + model.simpleClassName, e
                );
            }
        });
    }

    private void writeInjector(Map<String, Model> map) {
        String injectorPackageName = "ch.bytecrowd.injector";
        String injectorSimpleClassName = "PropertyInjector";
        String injectorClassName = injectorPackageName + "." + injectorSimpleClassName;
        try {
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(injectorClassName);
            Writer writer = javaFileObject.openWriter();

            writer.write("package " + injectorPackageName + ";\n\n");
            writer.write("\n");
            writer.write("public class " + injectorSimpleClassName + " {\n");
            writer.write("\n");
            writer.write("  public <T> T instantiate(Class<T> classToInstantiate) {\n");

            map.forEach((className, model) -> {
                try {
                    writer.write("      if (" + model.packageName + "." + model.simpleClassName + ".class.equals(classToInstantiate)) {\n");
                    writer.write("          return (T) " + model.packageName + "." + model.simpleClassName + "Factory.create();\n");
                    writer.write("      }\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("      throw new RuntimeException(\"no factory found for \" + classToInstantiate);\n");
            writer.write("  }\n");
            writer.write("}");
            writer.flush();

        } catch (IOException e) {

        }
    }
}