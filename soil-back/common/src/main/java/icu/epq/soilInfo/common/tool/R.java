package icu.epq.soilInfo.common.tool;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 *
 * @author EPQ
 */
@Data
@ToString
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private boolean success;
    private T data;
    private String msg;

    private R(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.success = code == 200;
    }

    private R(int code, String msg) {
        this(code, null, msg);
    }

    public static <T> R<T> data(T data) {
        return data(data, "操作成功");
    }

    private static <T> R<T> data(T data, String msg) {
        return data(200, data, msg);
    }

    private static <T> R<T> data(int code, T data, String msg) {
        return new R<>(code, data, data == null ? "暂无数据" : msg);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(400, msg);
    }

    public static <T> R<T> success(String msg) {
        return new R<>(200, msg);
    }

}
