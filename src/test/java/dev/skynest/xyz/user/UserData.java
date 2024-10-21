package dev.skynest.xyz.user;

import dev.skynest.xyz.interfaces.IData;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

@Setter
@Getter
public class UserData extends IData {

    private int money;
    private String id;

    public UserData(String name, int money) {
        super(name);
        this.money = money;
        this.id = RandomStringUtils.random(1000);
    }

    @Override
    protected void save() {
        // create the instance and do save(this)
    }
}
