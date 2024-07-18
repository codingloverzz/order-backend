package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public Result add(@RequestBody AddressBook addressBook) {
        addressBookService.insert(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        List<AddressBook> list = addressBookService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }
    @PutMapping
    public Result update(@RequestBody AddressBook addressBook){
        addressBookService.update(addressBook);
        return Result.success();
    }
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        addressBook.setIsDefault(1);
        addressBookService.setDefault(addressBook);
        return  Result.success();
    }
    @GetMapping("/default")
    public Result<AddressBook> getDefault(){
       AddressBook addressBook =  addressBookService.getDefault();
        return Result.success(addressBook);
    }
}
