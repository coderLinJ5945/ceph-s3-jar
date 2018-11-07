package com.ceph.s3.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;


@JsonSerialize(include =  JsonSerialize.Inclusion.NON_NULL)
public class HaoCangServerResponse<T> implements Serializable {
	
	private Integer status;
	
	private String msg;
	
	private T data;

	 
	private HaoCangServerResponse(Integer status){
		this.status = status;
	}
	private HaoCangServerResponse(Integer status, String msg){
		this.status = status;
		this.msg = msg;
	}
	private HaoCangServerResponse(Integer status, String msg, T data){
		this.status = status;
		this.msg = msg;
		this.data = data;
	}
	private HaoCangServerResponse(Integer status, T data){
		this.status = status;
		this.data = data;
	}
	
	@JsonIgnore
	public boolean isSucess(){
		return this.status == HaoCangResponseCodeEnum.SUCCESS.getCode(); //todo ,暂时用200代替，后续用枚举
	}
	
	
	public static<T> HaoCangServerResponse<T> createBySucess(){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.SUCCESS.getCode());
	}
	public static<T> HaoCangServerResponse<T> createBySucess(String msg){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.SUCCESS.getCode(),msg);
	}
	public static<T> HaoCangServerResponse<T> createBySucess(T data){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.SUCCESS.getCode(),data);
	}
	public static<T> HaoCangServerResponse<T> createBySucess(String msg, T data){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.SUCCESS.getCode(),msg,data);
	}
	public static<T> HaoCangServerResponse<T> createBySucess(Integer code, String msg, T data){
		return new HaoCangServerResponse<T>(code,msg,data);
	}
	
	public static<T> HaoCangServerResponse<T> createByError(){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.ERROR.getCode());
	}
	public static<T> HaoCangServerResponse<T> createByError(String msg){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.ERROR.getCode(), msg);
	}
	
	public static<T> HaoCangServerResponse<T> createByError(int code, String msg){
		return new HaoCangServerResponse<T>(code, msg);
	}
	public static<T> HaoCangServerResponse<T> createByError(String msg, T data){
		return new HaoCangServerResponse<T>(HaoCangResponseCodeEnum.ERROR.getCode(),msg,data);
	}
	
	public Integer getStatus() {
		return status;
	}
	 
	public String getMsg() {
		return msg;
	}
	 
	public T getData() {
		return data;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setData(T data) {
		this.data = data;
	}
}
