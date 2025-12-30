package com.example.erp_.factory;

import com.example.erp_.model.Supplier;

public class SupplierFactory {

    public enum SupplierType { MEI, LTDA, SA }

    public static Supplier create(SupplierType type, String name) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setActive(true);
        switch (type) {
            case MEI:
                s.setContact("MEI Contact");
                break;
            case LTDA:
                s.setContact("LTDA Contact");
                break;
            case SA:
                s.setContact("SA Contact");
                break;
            default:
                s.setContact("Contato");
        }
        return s;
    }
}

