package relake.account;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    public final List<Account> accounts = new ArrayList<>();
    @Getter
    @Setter
    private String lastLogin;

    public void add(String name) {
        accounts.add(new Account(name));
    }

    public void remove(String name) {
        accounts.removeIf(account -> account.getName().equalsIgnoreCase(name));
    }

    public void clear() {
        accounts.clear();
    }

    public boolean contains(String name) {
        return accounts.stream().anyMatch(account -> account.getName().equalsIgnoreCase(name));
    }
}
