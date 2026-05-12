package spring.aop;

public record Fingerprint(Class<?> targetType, int loaderId, long advisorDigest) {
}
