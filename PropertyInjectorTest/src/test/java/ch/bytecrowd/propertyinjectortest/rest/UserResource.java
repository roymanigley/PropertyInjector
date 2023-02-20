package ch.bytecrowd.propertyinjectortest.rest;

import ch.bytecrowd.processor.InjectedProperty;
import ch.bytecrowd.propertyinjectortest.service.UserService;

public class UserResource {

    private final UserService service;

    public UserResource(@InjectedProperty(singleton = true) UserService service) {
        this.service = service;
    }
}
