package org.arquillian.smart.testing.strategies.affected;

import java.nio.file.Path;

public class FileElement implements Element {

    private final Path fileLocation;

    FileElement(Path fileLocation) {
        this.fileLocation = fileLocation;
    }

    public Path getFileLocation() {
        return fileLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FileElement that = (FileElement) o;

        return fileLocation != null ? fileLocation.equals(that.fileLocation) : that.fileLocation == null;
    }

    @Override
    public int hashCode() {
        return fileLocation != null ? fileLocation.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileElement{");
        sb.append("fileLocation=").append(fileLocation);
        sb.append('}');
        return sb.toString();
    }
}
