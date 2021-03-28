package cz.cvut.kbss.termit.model;

import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Account extends AbstractUser {
	
	
    @JsonIgnore
    public boolean isLocked() {
        return types != null && types.contains(Vocabulary.s_c_uzamceny_uzivatel_termitu);
    }

    /**
     * Locks the account represented by this instance.
     */
    public void lock() {
        addType(Vocabulary.s_c_uzamceny_uzivatel_termitu);
    }

    /**
     * Unlocks the account represented by this instance.
     */
    public void unlock() {
        if (types == null) {
            return;
        }
        types.remove(Vocabulary.s_c_uzamceny_uzivatel_termitu);
    }

    /**
     * Enables the account represented by this instance.
     * <p>
     * Does nothing if the account is already enabled.
     */
    public void enable() {
        if (types == null) {
            return;
        }
        types.remove(Vocabulary.s_c_zablokovany_uzivatel_termitu);
    }

    /**
     * Checks whether the account represented by this instance is enabled.
     */
    @JsonIgnore
    public boolean isEnabled() {
        return types == null || !types.contains(Vocabulary.s_c_zablokovany_uzivatel_termitu);
    }

    /**
     * Disables the account represented by this instance.
     * <p>
     * Disabled account cannot be logged into and cannot be used to view/modify data.
     */
    public void disable() {
        addType(Vocabulary.s_c_zablokovany_uzivatel_termitu);
    }

    /**
     * Checks whether this account is administrator.
     *
     * @return {@code true} if this account is of administrator type
     */
    public boolean isAdmin() {
        return types != null && types.contains(Vocabulary.s_c_administrator_termitu);
    }

    /**
     * Transforms this security-related {@code UserAccount} instance to a domain-specific {@code User} instance.
     *
     * @return new user instance based on this account
     */
    public User toUser() {
        final User user = new User();
        copyAttributes(user);
        return user;
    }

    protected void copyAttributes(AbstractUser target) {
        target.setUri(uri);
        target.setFirstName(firstName);
        target.setLastName(lastName);
        target.setUsername(username);
        if (types != null) {
            target.setTypes(new HashSet<>(types));
        }
    }

    /**
     * Returns a copy of this user account.
     *
     * @return This instance's copy
     */
    public UserAccount copy() {
        final UserAccount clone = new UserAccount();
        copyAttributes(clone);
        clone.password = password;
        return clone;
    }
}

	

	 
	 
