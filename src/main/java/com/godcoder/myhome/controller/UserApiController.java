package com.godcoder.myhome.controller;


import com.godcoder.myhome.model.Board;
import com.godcoder.myhome.model.QUser;
import com.godcoder.myhome.model.User;
import com.godcoder.myhome.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
class UserApiController {

    @Autowired
    private UserRepository repository;

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/users")
    Iterable<User> all(@RequestParam(required = false) String method, @RequestParam(required = false) String text) {
        Iterable<User> users = null;
        if("query".equals(method)){
            users = repository.findByUsernameQuery(text);
        }else if("nativeQuery".equals(method)){
            users = repository.findByUsernameNativeQuery(text);
        }else if("querydsl".equals(method)){
            QUser user = QUser.user;
            Predicate predicate = user.username.contains(text);

            users = repository.findAll(predicate);
        }else if("querydslCustom".equals(method)){
            users = repository.findByUsernameCustom(text);
        }else if("jdbc".equals(method)){
            users = repository.findByUsernameJdbc(text);
        }else{
            users = repository.findAll();
        }
        return users;
    }
    // end::get-aggregate-root[]

    @PostMapping("/users")
    User newUser(@RequestBody User newUser) {
        return repository.save(newUser);
    }

    // Single item

    @GetMapping("/users/{id}")
    User one(@PathVariable Long id) {

        return repository.findById(id).orElse(null);
    }

    @PutMapping("/users/{id}")
    User replaceUser(@RequestBody User newUser, @PathVariable Long id) {

        return repository.findById(id)
                .map(User ->  {
//                    User.setTitle(newUser.getTitle());
//                    User.setContent(newUser.getContent());
                   // User.setBoards(newUser.getBoards());
                    User.getBoards().clear();
                    User.getBoards().addAll(newUser.getBoards());
                    for(Board board: User.getBoards()){
                        board.setUser(User);
                    }
                    return repository.save(User);
                })
                .orElseGet(() -> {
                    newUser.setId(id);
                    return repository.save(newUser);
                });
    }

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
