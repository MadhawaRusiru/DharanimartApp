package lk.dharanimart.mobile.Requests;

public class GetCategories {
    public String getAction() {
        return action;
    }

    public GetCategories(String client_id) {
        this.action = "GET_CATEGORIES";
        this.client_id = client_id;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    private String action;
    private String client_id;
}
