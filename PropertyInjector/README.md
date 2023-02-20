# PropertyInjector
> Simple dependency injection using annotation processing

## Limitations

- don't work for inner classes

## Example Usage

before you can use the dependency you have to build this project using `mvn clen install`

### `pom.xml`

    <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0">
        <modelVersion>4.0.0</modelVersion>
        <groupId>org.example</groupId>
        <artifactId>InjectorTest</artifactId>
        <version>1.0-SNAPSHOT</version>
        <name>InjectorTest Tapestry 5 Application</name>
    
        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    
            <maven.compiler.verson>3.5.1</maven.compiler.verson>
            <maven.compiler.source>17</maven.compiler.source>
            <maven.compiler.target>17</maven.compiler.target>
        </properties>
    
        <dependencies>
            <dependency>
                <groupId>ch.bytecrowd.processor</groupId>
                <artifactId>Processor</artifactId>
                <version>1.0.0</version>
            </dependency>
        </dependencies>
    
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${maven.compiler.verson}</version>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                    <configuration>
                        <source>${maven.compiler.source}</source>
                        <target>${maven.compiler.target}</target>
                        <encoding>UTF-8</encoding>
                        <generatedSourcesDirectory>${project.build.directory}/generated-sources/</generatedSourcesDirectory>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </project>


### `UserService.java`

    import ch.bytecrowd.processor.InjectedProperty;

    public class UserService {

        private final UserMapper mapper;
        private final UserRepository repository;
    
        public UserService(
                @InjectedProperty(dependencyInstantiation = "new org.example.service.mapper.UserMapper()") UserMapper mapper,
                @InjectedProperty(dependencyInstantiation = "new org.example.repository.UserRepository()") UserRepository repository
        ) {
            this.mapper = mapper;
            this.repository = repository;
        }
    }

### `App.java`

    import ch.bytecrowd.injector.PropertyInjector;
    
    public class App {
    
        public static void main(String[] args) {
            var service = new PropertyInjector().instantiate(UserService.class);
            ...
        }
    }


### build your application
> you have to run the package command twice since the generated-sources can not be found by maven in the first run  
> you probably need to add the folder `target/generated sources` to your IDE source path

    mvn clean package || mvn package

## How it works
annotate the dependencies in the constructor with `@InjectedProperty`

    public UserService(
            @InjectedProperty(dependencyInstantiation = "new org.example.service.mapper.UserMapper()") UserMapper mapper,
            @InjectedProperty(dependencyInstantiation = "new org.example.repository.UserRepository()") UserRepository repository
    ) {
        this.mapper = mapper;
        this.repository = repository;
    }

this will generate a `UserServiceFactory` when you then need the `UserService` you don't need to specify how it is instantiated since it will be instantiated using the `UserServiceFactory`

    public UserResource(
        @InjectedProperty(singleton = true) UserService service
    ) {
        this.service = service;
    }

All Classes with the Annotation `@InjectedProperty` can be created using the generated `PropertyInjector`

    var injector = new PropertyInjector();
    var userService = injector.instantiate(UserService.class);
    var userResource = injector.instantiate(UserResource.class);
