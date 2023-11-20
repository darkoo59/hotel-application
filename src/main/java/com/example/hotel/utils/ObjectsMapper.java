package com.example.hotel.utils;

import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class ObjectsMapper {
    private static final ModelMapper modelMAPPER = new ModelMapper();

    public static User convertRegisterDTOToUser(RegisterBodyDTO registerDTO) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<RegisterBodyDTO, User> answerMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                map().setFirstname(source.getFirstname());
                map().setLastname(source.getLastname());
                map().setEmail(source.getEmail());
                map().setPassword(source.getPassword());
                map().setPhone(source.getPhone());
                map().setRole(source.getRole());
                map().setAddress(source.getAddress());
                map().setSex(source.getSex());
                map().setBirthdate(source.getBirthdate());
            }
        };

        modelMapper.addMappings(answerMap);
        return modelMapper.map(registerDTO, User.class);
    }


}
