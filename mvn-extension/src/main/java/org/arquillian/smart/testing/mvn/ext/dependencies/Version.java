package org.arquillian.smart.testing.mvn.ext.dependencies;

/**
 * Version class to represent a version of an artifact. Notice that we are just supporting the basic fields of
 * semantic version {@link http://semver.org/}. Prerelease tags supported are SNAPSHOT, rc and M.
 */
public class Version implements Comparable<Version> {

    private String fullVersion;

    private Integer major;
    private Integer minor;
    private Integer patch = 0;

    private PreRelease preRelease;

    private Version() {
    }

    public static Version from(String version) {

        Version versionObject = new Version();
        versionObject.fullVersion = version;

        final String[] fields = version.split("-");

        final String[] versions = fields[0].split("\\.");
        versionObject.major = Integer.parseInt(versions[0]);
        versionObject.minor = Integer.parseInt(versions[1]);

        if (versions.length == 3) {
            versionObject.patch = Integer.parseInt(versions[2]);
        }

        if (containsPreReleaseInfo(fields)) {
            versionObject.preRelease = PreRelease.from(fields[1]);
        }

        return versionObject;

    }

    private static boolean containsPreReleaseInfo(String[] fields) {
        return fields.length == 2;
    }

    @Override
    public String toString() {
        return fullVersion;
    }

    public boolean isGreaterOrEqualThan(Version version) {
        return this.compareTo(version) >= 0;
    }

    @Override
    public int compareTo(Version v) {

        final int comparisionMajor = this.major.compareTo(v.major);
        if (comparisionMajor == 0) {
            final int comparisionMinor = this.minor.compareTo(v.minor);

            if (comparisionMinor == 0) {
                final int comparisionPatch = this.patch.compareTo(v.patch);

                if (comparisionPatch == 0) {
                    if (this.preRelease != null && v.preRelease != null) {
                        return this.preRelease.compareTo(v.preRelease);
                    } else {
                        if (this.preRelease == null && v.preRelease != null) {
                            return 1;
                        } else {
                            if (this.preRelease != null) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }
                } else {
                    return comparisionPatch;
                }

            } else {
                return comparisionMinor;
            }

        } else {
            return comparisionMajor;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Version version = (Version) o;

        if (major != null ? !major.equals(version.major) : version.major != null) return false;
        if (minor != null ? !minor.equals(version.minor) : version.minor != null) return false;
        if (patch != null ? !patch.equals(version.patch) : version.patch != null) return false;
        return preRelease != null ? preRelease.equals(version.preRelease) : version.preRelease == null;
    }

    @Override
    public int hashCode() {
        int result = major != null ? major.hashCode() : 0;
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        result = 31 * result + (patch != null ? patch.hashCode() : 0);
        result = 31 * result + (preRelease != null ? preRelease.hashCode() : 0);
        return result;
    }

    public static class PreRelease implements Comparable<PreRelease> {
        private PreReleaseTag tag;
        private Integer preReleaseVersion;

        private PreRelease(){
        }

        public static PreRelease from(String preRelease) {
            PreRelease preReleaseObject = new PreRelease();

            if ("SNAPSHOT".equals(preRelease)) {
                preReleaseObject.tag = PreReleaseTag.SNAPSHOT;
            } else {
                if (preRelease.startsWith("M")) {
                    preReleaseObject.tag = PreReleaseTag.MILESTONE;
                    try {
                        preReleaseObject.preReleaseVersion = Integer.parseInt(preRelease.substring(1));
                    } catch (Exception e) {
                        preReleaseObject.preReleaseVersion = 0;
                    }
                } else {
                    if (preRelease.startsWith("rc")) {
                        preReleaseObject.tag = PreReleaseTag.RC;
                        try {
                            preReleaseObject.preReleaseVersion = Integer.parseInt(preRelease.substring(3));
                        } catch (Exception e) {
                            preReleaseObject.preReleaseVersion = 0;
                        }
                    }
                }
            }

            return preReleaseObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PreRelease that = (PreRelease) o;

            if (tag != that.tag) return false;
            return preReleaseVersion != null ? preReleaseVersion.equals(that.preReleaseVersion)
                : that.preReleaseVersion == null;
        }

        @Override
        public int hashCode() {
            int result = tag != null ? tag.hashCode() : 0;
            result = 31 * result + (preReleaseVersion != null ? preReleaseVersion.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(PreRelease r) {

            final int comparision = this.tag.compareTo(r.tag);

            if (comparision == 0) {
                return this.preReleaseVersion.compareTo(r.preReleaseVersion);
            } else {
                return comparision;
            }
        }

    }

    enum PreReleaseTag {
        SNAPSHOT, RC, MILESTONE
    }

}
