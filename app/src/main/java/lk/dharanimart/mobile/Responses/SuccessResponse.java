package lk.dharanimart.mobile.Responses;

import java.util.List;

public class SuccessResponse<E> {
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data<E> getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    private Boolean success;
    private String msg;
    private Data<E> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private int total;

    public class Data<E>{
        public List<E> getList() {
            return list;
        }

        public void setList(List<E> list) {
            this.list = list;
        }

        private List<E> list;

    }
}
