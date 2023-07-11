import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class DiscordBot extends ListenerAdapter {
    public static final int MIN_SIZE = 112, MAX_SIZE = 256, DEFAULT_SIZE = MIN_SIZE, MIN_SPEED = 20, MAX_SPEED = 100, DEFAULT_SPEED = 50;
    public static final float[] ANIMATION = new float[] { -.05f, .1f, .2f, .19f, .1f };
    public static final float MAX_ANIMATION_VALUE;
    public static final RenderingHints RH = new RenderingHints(new HashMap<RenderingHints.Key, Object>() {{
        put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }});
    public final BufferedImage[] frames;

    static {
        float max = 0;
        for (final float v : ANIMATION)
            if (v > max)
                max = v;
        MAX_ANIMATION_VALUE = max;
    }

    public static class SlashBuilder {
        public final SlashCommandData[] c;

        public SlashBuilder(final String description, final String... aliases) {
            c = new SlashCommandData[aliases.length];
            for (int i = 0; i < c.length; i++)
                c[i] = Commands.slash(aliases[i], description);
        }

        public SlashBuilder addOption(final OptionType type, final String name, final String description, final boolean required) {
            for (final SlashCommandData c : c)
                c.addOption(type, name, description, required);
            return this;
        }

        public SlashCommandData[] get() { return c; }
    }

    public static void main(final String[] args) {
        try {
            final JDA jda;
            try (final Scanner s = new Scanner(new File("token.txt"))) {
                jda = JDABuilder.createDefault(s.nextLine())
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .build();
            }
            final CommandListUpdateAction clu = jda.updateCommands()
                            .addCommands(
                                    Commands.slash("pet", "Pet")
                                            .addOption(OptionType.USER, "user", "The user to PET", false)
                                            .addOption(OptionType.INTEGER, "resolution", "GIF resolution. " + MIN_SIZE + " - min, " + MAX_SIZE + " - max, " + DEFAULT_SIZE + " - default", false)
                                            .addOption(OptionType.INTEGER, "timebetweenframesms", "Time between frames in milliseconds. " + MIN_SPEED + " - faster, " + MAX_SPEED + " - slower, " + DEFAULT_SPEED + " - default", false)
                                            .addOption(OptionType.BOOLEAN, "scalex", "Stretch along the X axis. True - default", false)
                                            .addOption(OptionType.BOOLEAN, "centerbyx", "Center the avatar on the X axis. True - default", false)
                            );
            try {
                new SlashBuilder("Гладити", "гладити", "погладити") {{
                        addOption(OptionType.USER, "user", "Кого треба погладити", false);
                        addOption(OptionType.INTEGER, "resolution", "Роздільна здатність ґіфу. " + MIN_SIZE + " - мінімум, " + MAX_SIZE + " - максимум, " + DEFAULT_SIZE + " - за замовчуванням", false);
                        addOption(OptionType.INTEGER, "timebetweenframesms", "Час між кадрами в мілісекундах. " + MIN_SPEED + " - швидше, " + MAX_SPEED + " - повільніше, " + DEFAULT_SPEED + " - за замовчуванням", false);
                        addOption(OptionType.BOOLEAN, "scalex", "Розтягувати по осі X. True - за замовчуванням", false).addOption(OptionType.BOOLEAN, "centerbyx", "Центрувати аватар по осі Х. True - за замовчуванням", false);
                        clu.addCommands(get());
                    }};
            } catch (final IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
            clu.queue();
            jda.addEventListener(new DiscordBot());
            System.gc();
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public DiscordBot() throws Exception {
        final ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        try (final ImageInputStream iis = ImageIO.createImageInputStream(new File("hand.gif"))) {
            reader.setInput(iis, false);

            frames = new BufferedImage[reader.getNumImages(true)];
            for (int i = 0; i < frames.length; i++)
                frames[i] = reader.read(i);
        }
        reader.dispose();
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        boolean Uk_UA = true;
        switch (event.getName()) {
            case "pet":
                Uk_UA = false;
            case "погладити": case "гладити":
                try {
                    User target = event.getOption("user", OptionMapping::getAsUser);
                    if (target == null)
                        target = event.getUser();
                    final OptionMapping mapping = event.getOption("resolution"), mappingSpeed = event.getOption("timebetweenframesms"),
                            mappingScaleX = event.getOption("scalex"), mappingCenterByX = event.getOption("centerbyx");
                    final int size = mapping == null ? DEFAULT_SIZE : Math.min(Math.max(mapping.getAsInt(), MIN_SIZE), MAX_SIZE), force = size / 2, offset = force / 2,
                            speed = mappingSpeed == null ? DEFAULT_SPEED : Math.min(Math.max(mappingSpeed.getAsInt(), MIN_SPEED), MAX_SPEED);
                    final boolean x = mappingScaleX == null || mappingScaleX.getAsBoolean(), cx = mappingCenterByX == null || mappingCenterByX.getAsBoolean();
                    final ImageProxy avatar = target.getAvatar();
                    if (avatar == null)
                        if (Uk_UA)
                            throw new Exception("Немає аватарки");
                        else
                            throw new Exception("There is no avatar");
                    final BufferedImage ava = ImageIO.read(avatar.download().get());
                    try (
                            final ByteArrayOutputStream os = new ByteArrayOutputStream();
                            final ImageOutputStream ios = ImageIO.createImageOutputStream(os)
                    ) {
                        try (final GIFMaker g = new GIFMaker(ios, BufferedImage.TYPE_INT_ARGB, speed, true, true, GIFMaker.RESTORE_TO_BACKGROUND_COLOR)) {
                            for (int i = 0; i < frames.length; i++) {
                                final BufferedImage frame = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                                final Graphics2D gr = (Graphics2D) frame.getGraphics();
                                gr.setRenderingHints(RH);
                                final int c1 = Math.round(ANIMATION[i] * force), c2 = x ? Math.round((MAX_ANIMATION_VALUE - ANIMATION[i]) * force) : 0;
                                gr.drawImage(ava, cx ? c2 / 2 : 0, offset + c1, size - c2, size - offset - c1, null);
                                gr.drawImage(frames[i], 0, 0, size, size, null);
                                gr.dispose();
                                g.write(frame);
                            }
                        }
                        ios.flush();
                        event.replyFiles(FileUpload.fromData(os.toByteArray(), target.getAvatarId() + ".gif")).queue();
                    }
                } catch (final Exception ex) {
                    final String msg = ex.getMessage();
                    if (msg != null && msg.length() > 0) {
                        event.reply(msg).queue();
                        return;
                    }
                    if (Uk_UA)
                        event.reply("Помилка").queue();
                    else
                        event.reply("Error").queue();
                    ex.printStackTrace();
                }
                break;
        }
    }
}