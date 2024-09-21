package dev.skynest.xyz.user;

import dev.skynest.xyz.interfaces.IData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserData extends IData {

    private int money;

    public UserData(String name, int money) {
        super(name);
        this.money = money;
    }

    @Override
    protected void save() {
        // create the instance and do save(this)
    }
}
