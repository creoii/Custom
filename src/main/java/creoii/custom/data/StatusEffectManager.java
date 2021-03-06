package creoii.custom.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import creoii.custom.custom.CustomStatusEffect;

import java.io.Reader;

public class StatusEffectManager extends AbstractDataManager<CustomStatusEffect> {
    public StatusEffectManager() {
        super("status_effects", new GsonBuilder().setPrettyPrinting().registerTypeAdapter(CustomStatusEffect.class, new CustomStatusEffect.Serializer()).create());
    }

    @Override
    CustomStatusEffect createCustomObject(Reader reader, Gson gson) {
        return gson.fromJson(reader, CustomStatusEffect.class);
    }
}
