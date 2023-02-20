package ch.bytecrowd.propertyinjectortest.service;

import ch.bytecrowd.processor.InjectedProperty;
import ch.bytecrowd.propertyinjectortest.repository.UserRepository;
import ch.bytecrowd.propertyinjectortest.service.mapper.UserMapper;

public class UserService {

    private final UserMapper mapper;
    private final UserRepository repository;

    public UserService(
            @InjectedProperty(dependencyInstantiation = "new ch.bytecrowd.propertyinjectortest.service.mapper.UserMapper()") UserMapper mapper,
            @InjectedProperty(dependencyInstantiation = "new ch.bytecrowd.propertyinjectortest.repository.UserRepository()") UserRepository repository
    ) {
        this.mapper = mapper;
        this.repository = repository;
    }
}
