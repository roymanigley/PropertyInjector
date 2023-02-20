package ch.bytecrowd.propertyinjectortest;

import ch.bytecrowd.injector.PropertyInjector;
import ch.bytecrowd.propertyinjectortest.repository.UserRepository;
import ch.bytecrowd.propertyinjectortest.rest.UserResource;
import ch.bytecrowd.propertyinjectortest.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyInjectorTest {

    @Test
    void testInjectionWhenFound() {
        var service = new PropertyInjector().instantiate(UserService.class);
        var resource = new PropertyInjector().instantiate(UserResource.class);

        assertThat(service).isNotNull();
        assertThat(resource).isNotNull();
    }

    @Test
    void testInjectionWhenNotFound() {
        Assertions.assertThrows(RuntimeException.class,
                () -> new PropertyInjector().instantiate(UserRepository.class)
        );
    }
}
