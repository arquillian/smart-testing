package org.arquillian.smart.testing.scm;

import java.nio.file.Path;

public class Change {

    private Path location;
    private ChangeType changeType;

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
        final StringBuilder sb = new StringBuilder("Change{");
        sb.append("location='").append(location).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append('}');
        return sb.toString();
    }
}
