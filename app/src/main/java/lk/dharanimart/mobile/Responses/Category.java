package lk.dharanimart.mobile.Responses;

import java.util.List;

public class Category {

    private int id;
    private String name;
    private String icon;
    private int total;
    private List<SubCategory> subcat;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<SubCategory> getSubcat() {
        return subcat;
    }
    public void setSubcat(List<SubCategory> subcat) {
        this.subcat = subcat;
    }

    public class SubCategory {
        private int id;
        private String name;
        private int total;
        private List<LowerSubCategory> lower;

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getTotal() {
            return total;
        }
        public void setTotal(int total) {
            this.total = total;
        }
        public List<LowerSubCategory> getLower() {
            return lower;
        }
        public void setLower(List<LowerSubCategory> lower) {
            this.lower = lower;
        }

        public class LowerSubCategory {
            private int id;
            private String name;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

}
