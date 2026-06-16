package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Address;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {
    List<Address> findByUserId(Long userId);

    Address findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Address findDefaultByUserId(Long userId);

    int insert(Address address);

    int update(Address address);

    int clearDefault(Long userId);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
