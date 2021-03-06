package co.ReaperDev.service;

import co.ReaperDev.dto.UserDTO;
import co.ReaperDev.exception.UserNameOrEmailAlreadyRegistered;
import co.ReaperDev.exception.UserNotFoundException;
import co.ReaperDev.repository.UserRepository;
import co.ReaperDev.repository.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private UserRepository repository;
    private MapperFacade mapper;

    @Autowired
    public UserService(UserRepository r){
        this.repository = r;
    }

    public void createUser(UserDTO userDTO){
        log.info("UserService.createUser(" + userDTO.toString() + ")");
        UserEntity entity = new UserEntity(userDTO.getEmail(), userDTO.getUserName(), userDTO.getPassword());

        try {
            repository.createUser(entity);
        }catch (Exception ex){
            throw new UserNameOrEmailAlreadyRegistered();
        }
    }

    public UserDTO userLogin(UserDTO userDTO){
        log.info("UserService.userLogin()");
        log.info(userDTO.toString());
        UserEntity entity =  new UserEntity(userDTO.getEmail(), userDTO.getUserName(), userDTO.getPassword());
        log.info(entity.toString());
        log.info("entity mapped");

        UserEntity retval;

        try{
            retval = repository.userLogin(entity);
        }catch (EmptyResultDataAccessException ex){
            throw new UserNotFoundException(userDTO.getUserName());
        }


        return mapper.map(retval, UserDTO.class);

    }
    public List<UserDTO> getAllUsers(){
        log.info("UserService.getAllUsers()");
        List<UserEntity> entities = repository.getAllUsers();
        List<UserDTO> retval = new ArrayList<>();
        for(UserEntity e: entities){
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName(e.getUserName());
            userDTO.setEmail(e.getEmail());
            retval.add(userDTO);
        }
        return retval;
    }
}
