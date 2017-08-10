package org.arquillian.smart.testing.scm;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.arquillian.smart.testing.scm.ChangeType.ADD;
import static org.arquillian.smart.testing.scm.ChangeType.MODIFY;

public class Change {

    private final Path location;
    private final ChangeType changeType;

    public Change(Path location, ChangeType changeType) {
        this.location = location;
        this.changeType = changeType;
    }

    public Path getLocation() {
        return location;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public static Change add(String root, String location) {
        return new Change(Paths.get(root, location), ADD);
    }

    public static Change modify(String root, String location) {
        return new Change(Paths.get(root, location), MODIFY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Change)) {
            return false;
        }

        final Change change = (Change) o;

        return location != null ? location.equals(change.location) : change.location == null;
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Change{" + "location='" + location + '\'' + ", changeType=" + changeType + '}';
    }

    // FIXME or maybe just Serializable - so we produce binary
    public String write() {
        return "" + location + ',' + changeType;
    }

    public static Change read(String serialized) {
        final String[] parts = serialized.split(",");
        return new Change(Paths.get(parts[0]), ChangeType.valueOf(parts[1]));
    }
}
