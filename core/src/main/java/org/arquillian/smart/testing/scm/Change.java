package org.arquillian.smart.testing.scm;

import java.io.File;

public class Change {

    private File location;
    private ChangeType changeType;

    public Change(File location, ChangeType changeType) {
        this.location = location;
        this.changeType = changeType;
    }

    public File getLocation() {
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
        sb.append("location='").append(location.getAbsolutePath()).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append('}');
        return sb.toString();
    }
}
