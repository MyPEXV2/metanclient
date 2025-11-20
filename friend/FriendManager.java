package relake.friend;

import java.util.concurrent.CopyOnWriteArrayList;

public class FriendManager {
    public final CopyOnWriteArrayList<Friend> friends = new CopyOnWriteArrayList<>();

    public void addFriend(String name) {
        friends.add(new Friend(name));
    }

    public void removeFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public void clearFriends() {
        friends.clear();
    }
}
