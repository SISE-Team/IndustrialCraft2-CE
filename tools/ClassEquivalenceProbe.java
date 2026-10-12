import java.util.Arrays;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

class ClassEquivalenceProbe {
    private static byte[] rebuild(byte[] input) {
        var writer = new ClassWriter(0);
        new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        super.visitLocalVariable("slot"+index, descriptor, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        int checked = 0, reordered = 0;
        try (var old = new ZipFile(args[0]); var updated = new ZipFile(args[1])) {
            var names = new TreeSet<String>();
            old.stream().filter(e -> e.getName().endsWith(".class")).forEach(e -> names.add(e.getName()));
            var newer = new TreeSet<String>();
            updated.stream().filter(e -> e.getName().endsWith(".class")).forEach(e -> newer.add(e.getName()));
            if (!names.equals(newer)) throw new AssertionError("Class sets differ");
            for (var name : names) {
                byte[] a = old.getInputStream(old.getEntry(name)).readAllBytes();
                byte[] b = updated.getInputStream(updated.getEntry(name)).readAllBytes();
                if (!Arrays.equals(a, b)) {
                    if (!Arrays.equals(rebuild(a), rebuild(b))) throw new AssertionError("Class model differs: " + name);
                    reordered++;
                }
                checked++;
            }
        }
        System.out.println("{\"classes\":"+checked+",\"debugLocalNameDifferences\":"+reordered+",\"executableClassDifferences\":0}");
    }
}
