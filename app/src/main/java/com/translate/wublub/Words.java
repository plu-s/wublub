package com.translate.wublub;

import org.litepal.crud.DataSupport;


public class Words extends DataSupport {

        private int ids;

        private String src;

        private String translation;

        public int getId() {
            return ids;
        }

        public void setId(int id) {
            this.ids = id;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }


}
