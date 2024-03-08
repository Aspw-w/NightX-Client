package net.aspw.client.injection.transformers;

import net.aspw.client.Launch;
import net.aspw.client.features.module.impl.other.BrandSpoofer;
import net.aspw.client.utils.ClassUtils;
import net.aspw.client.utils.NodeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.*;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ForgeNetworkTransformer implements IClassTransformer {

    public static boolean returnMethod() {
        return Objects.requireNonNull(Launch.moduleManager.getModule(BrandSpoofer.class)).getState() && !Minecraft.getMinecraft().isIntegratedServerRunning();
    }

    /**
     * Transform a class
     *
     * @param name            of target class
     * @param transformedName of target class
     * @param basicClass      bytecode of target class
     * @return new bytecode
     */
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.equals("net.minecraftforge.fml.common.network.handshake.NetworkDispatcher")) {
            try {
                final ClassNode classNode = ClassUtils.INSTANCE.toClassNode(basicClass);

                classNode.methods.stream().filter(methodNode -> methodNode.name.equals("handleVanilla")).forEach(methodNode -> {
                    final LabelNode labelNode = new LabelNode();

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), NodeUtils.INSTANCE.toNodes(
                            new MethodInsnNode(INVOKESTATIC, "net/aspw/client/injection/transformers/ForgeNetworkTransformer", "returnMethod", "()Z", false),
                            new JumpInsnNode(IFEQ, labelNode),
                            new InsnNode(ICONST_0),
                            new InsnNode(IRETURN),
                            labelNode
                    ));
                });

                return ClassUtils.INSTANCE.toBytes(classNode);
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        if (name.equals("net.minecraftforge.fml.common.network.handshake.HandshakeMessageHandler")) {
            try {
                final ClassNode classNode = ClassUtils.INSTANCE.toClassNode(basicClass);

                classNode.methods.stream().filter(method -> method.name.equals("channelRead0")).forEach(methodNode -> {
                    final LabelNode labelNode = new LabelNode();

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), NodeUtils.INSTANCE.toNodes(
                            new MethodInsnNode(INVOKESTATIC,
                                    "net/aspw/client/injection/transformers/ForgeNetworkTransformer",
                                    "returnMethod", "()Z", false
                            ),
                            new JumpInsnNode(IFEQ, labelNode),
                            new InsnNode(RETURN),
                            labelNode
                    ));
                });

                return ClassUtils.INSTANCE.toBytes(classNode);
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return basicClass;
    }
}