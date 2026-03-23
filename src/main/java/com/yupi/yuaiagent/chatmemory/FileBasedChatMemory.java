package com.yupi.yuaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemoryRepository {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }

    @Override
    public List<String> findConversationIds() {
        File dir = new File(BASE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".kryo"));
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(files)
                .map(file -> file.getName().replace(".kryo", ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        saveConversation(conversationId, messages);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }
}
