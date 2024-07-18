package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    void insert(AddressBook addressBook);

    List<AddressBook> getListByUserId(Long userId);

    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    void cancelDefault(Long userId);

    @Select("select * from address_book where is_default = 1")
    AddressBook getDefault();
}
