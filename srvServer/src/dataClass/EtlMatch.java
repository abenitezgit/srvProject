/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataClass;

/**
 *
 * @author andresbenitez
 */
public class EtlMatch {
    int EtlOrder;
    int EtlEnable;
    String EtlSourceField;
    int EtlSourceLength;
    String EtlSourceType;
    String EtlDestField;
    int EtlDestLength;
    String EtlDestType;
    
    //Getter ans Setter

    public int getEtlOrder() {
        return EtlOrder;
    }

    public void setEtlOrder(int EtlOrder) {
        this.EtlOrder = EtlOrder;
    }

    public int getEtlEnable() {
        return EtlEnable;
    }

    public void setEtlEnable(int EtlEnable) {
        this.EtlEnable = EtlEnable;
    }

    public String getEtlSourceField() {
        return EtlSourceField;
    }

    public void setEtlSourceField(String EtlSourceField) {
        this.EtlSourceField = EtlSourceField;
    }

    public int getEtlSourceLength() {
        return EtlSourceLength;
    }

    public void setEtlSourceLength(int EtlSourceLength) {
        this.EtlSourceLength = EtlSourceLength;
    }

    public String getEtlSourceType() {
        return EtlSourceType;
    }

    public void setEtlSourceType(String EtlSourceType) {
        this.EtlSourceType = EtlSourceType;
    }

    public String getEtlDestField() {
        return EtlDestField;
    }

    public void setEtlDestField(String EtlDestField) {
        this.EtlDestField = EtlDestField;
    }

    public int getEtlDestLength() {
        return EtlDestLength;
    }

    public void setEtlDestLength(int EtlDestLength) {
        this.EtlDestLength = EtlDestLength;
    }

    public String getEtlDestType() {
        return EtlDestType;
    }

    public void setEtlDestType(String EtlDestType) {
        this.EtlDestType = EtlDestType;
    }
    
    
}
