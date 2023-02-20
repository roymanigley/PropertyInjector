package ch.bytecrowd.propertyinjectortest;

import ch.bytecrowd.propertyinjectortest.service.UserServiceFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryTest {

    @Test
    void testCreateSingleton() {
        var singletonOne = UserServiceFactory.createSingleton();
        var singletonTwo = UserServiceFactory.createSingleton();

        assertThat(singletonOne).isSameAs(singletonTwo);
    }

    @Test
    void testCreate() {
        var singletonOne = UserServiceFactory.create();
        var singletonTwo = UserServiceFactory.create();

        assertThat(singletonOne).isNotSameAs(singletonTwo);
    }
}
