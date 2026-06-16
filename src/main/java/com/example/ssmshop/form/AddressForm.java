package com.example.ssmshop.form;

import jakarta.validation.constraints.NotBlank;

public class AddressForm {
    @NotBlank(message = "请输入收货人")
    private String receiver;
    @NotBlank(message = "请输入手机号")
    private String phone;
    @NotBlank(message = "请输入省份")
    private String province;
    @NotBlank(message = "请输入城市")
    private String city;
    @NotBlank(message = "请输入区县")
    private String district;
    @NotBlank(message = "请输入详细地址")
    private String detail;
    private Boolean defaultAddress = false;

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}
