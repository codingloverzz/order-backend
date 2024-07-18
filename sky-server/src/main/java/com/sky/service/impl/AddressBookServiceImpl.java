package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public void insert(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);

    }

    @Override
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.getListByUserId(userId);
        return list;
    }

    @Override
    public AddressBook getById(Long id) {

        return addressBookMapper.getById(id);
    }

    @Override
    public void update(AddressBook addressBook) {

        addressBookMapper.update(addressBook);
    }

    @Override
    public void setDefault(AddressBook addressBook) {
        //先把其他地址取消默认值
        addressBookMapper.cancelDefault(BaseContext.getCurrentId());
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook getDefault() {


        return addressBookMapper.getDefault();
    }
}
