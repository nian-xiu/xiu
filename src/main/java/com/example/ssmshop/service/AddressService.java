package com.example.ssmshop.service;

import com.example.ssmshop.domain.Address;
import com.example.ssmshop.form.AddressForm;
import com.example.ssmshop.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {
    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public List<Address> list(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    public Address find(Long id, Long userId) {
        return addressMapper.findByIdAndUserId(id, userId);
    }

    public Address defaultAddress(Long userId) {
        Address address = addressMapper.findDefaultByUserId(userId);
        if (address != null) {
            return address;
        }
        List<Address> addresses = list(userId);
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    @Transactional
    public void save(Long userId, Long id, AddressForm form) {
        if (Boolean.TRUE.equals(form.getDefaultAddress())) {
            addressMapper.clearDefault(userId);
        }
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiver(form.getReceiver());
        address.setPhone(form.getPhone());
        address.setProvince(form.getProvince());
        address.setCity(form.getCity());
        address.setDistrict(form.getDistrict());
        address.setDetail(form.getDetail());
        address.setDefaultAddress(Boolean.TRUE.equals(form.getDefaultAddress()));
        if (id == null) {
            addressMapper.insert(address);
        } else {
            addressMapper.update(address);
        }
    }

    public AddressForm toForm(Address address) {
        AddressForm form = new AddressForm();
        form.setReceiver(address.getReceiver());
        form.setPhone(address.getPhone());
        form.setProvince(address.getProvince());
        form.setCity(address.getCity());
        form.setDistrict(address.getDistrict());
        form.setDetail(address.getDetail());
        form.setDefaultAddress(address.getDefaultAddress());
        return form;
    }

    public void delete(Long id, Long userId) {
        addressMapper.deleteByIdAndUserId(id, userId);
    }
}
