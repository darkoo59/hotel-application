package com.example.hotel.utils;

import com.example.hotel.user.dto.RegisterBodyDTO;
import com.example.hotel.user.model.Guest;
import com.example.hotel.user.model.Host;
import com.example.hotel.user.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class ObjectsMapper {
    public static Guest convertRegisterDTOToGuest(RegisterBodyDTO registerBodyDTO) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<RegisterBodyDTO, Guest> answerMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                map().setFirstname(source.getFirstname());
                map().setLastname(source.getLastname());
                map().setEmail(source.getEmail());
                map().setPassword(source.getPassword());
                map().setPhone(source.getPhone());
                map().setAddress(source.getAddress());
                map().setSex(source.getSex());
                map().setBirthdate(source.getBirthdate());
                map().setEnabled(false);
            }
        };

        modelMapper.addMappings(answerMap);
        return modelMapper.map(registerBodyDTO, Guest.class);
    }

    public static Host convertRegisterDTOToHost(RegisterBodyDTO registerBodyDTO) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<RegisterBodyDTO, Host> answerMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                map().setFirstname(source.getFirstname());
                map().setLastname(source.getLastname());
                map().setEmail(source.getEmail());
                map().setPassword(source.getPassword());
                map().setPhone(source.getPhone());
                map().setAddress(source.getAddress());
                map().setSex(source.getSex());
                map().setBirthdate(source.getBirthdate());
                map().setEnabled(false);
            }
        };

        modelMapper.addMappings(answerMap);
        return modelMapper.map(registerBodyDTO, Host.class);
    }
}
