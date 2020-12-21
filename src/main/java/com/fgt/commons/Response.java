package com.fgt.commons;


import java.io.Serializable;


public class Response<T> implements Serializable {

    private static final int CODE_SUCCESS = 200;//请求成功的响应码

    private static final int CODE_FAIL = 500;//请求失败的响应码

    private static final String MSG_SUCCESS = "OK";//请求成功的消息

    private static final String MSG_FAIL = "error";//请求失败的消息


    private int code;//响应码

    private String msg;//返回的消息

    private T URL;//返回的数据


    public Response(int code) {
        this.code = code;
    }


    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(int code, String msg, T entity) {
        this.code = code;
        this.msg = msg;
        this.URL = entity;
    }

    public static Response success() {
        return new Response(CODE_SUCCESS, MSG_SUCCESS);
    }

    public static Response success(Object data) {
        return new Response(CODE_SUCCESS, MSG_SUCCESS, data);
    }


    public static Response fail() {
        return new Response(CODE_FAIL, MSG_FAIL);
    }


    public static Response fail(String msg) {
        return new Response(CODE_FAIL, msg);
    }


    public static Response fail(Object data) {
        return new Response(CODE_FAIL, MSG_FAIL, data);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setData(T data) {
        this.URL = data;
    }

    public T getData() {
        return URL;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}




