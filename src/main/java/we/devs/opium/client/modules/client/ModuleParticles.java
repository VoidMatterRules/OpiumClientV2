package we.devs.opium.client.modules.client;

import we.devs.opium.Opium;
import we.devs.opium.Opium;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.events.EventClient;
import we.devs.opium.client.events.EventRender2D;
import we.devs.opium.client.gui.particles.ParticleSystem;
import we.devs.opium.client.values.Value;
import we.devs.opium.client.values.impl.ValueColor;
import we.devs.opium.client.values.impl.ValueNumber;

import java.awt.*;

@RegisterModule(name="Particles", description="Render particles in gui screens.", category=Module.Category.CLIENT)
public class ModuleParticles extends Module {
    public static ModuleParticles INSTANCE;
    public ValueColor color = new ValueColor("ParticleColor", "Color", "", new Color(255, 255, 255));
    public ValueNumber size = new ValueNumber("Size", "Size", "", 1.0f, 0.5f, 5.0f);
    public ValueNumber lineWidth = new ValueNumber("LineWidth", "Line Width", "", 2.0, 1.0, 3.0);
    public ValueNumber amount = new ValueNumber("Population", "Population", "", 100, 50, 400);
    public ValueNumber radius = new ValueNumber("Radius", "Radius", "", 100, 50, 300);
    public ValueNumber speed = new ValueNumber("Speed", "Speed", "", 0.1f, 0.1f, 10.0f);
    public ValueNumber delta = new ValueNumber("Delta", "Delta", "", 1, 1, 10);
    boolean updateParticles = false;
    private ParticleSystem ps;

    public ModuleParticles() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.ps = new ParticleSystem(this.amount.getValue().intValue(), this.radius.getValue().intValue());
        this.ps.tick(this.delta.getValue().intValue());
        this.ps.dist = this.radius.getValue().intValue();
        ParticleSystem.SPEED = (float)this.speed.getValue().doubleValue();
    }

    @Override
    public void onClient(EventClient event) {
        if (this.nullCheck()) {
            return;
        }
        Value value = event.getValue();
        if (value != null && value == this.amount) {
            this.updateParticles = true;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.nullCheck()) {
            return;
        }
        if (this.updateParticles) {
            this.ps.changeParticles(this.amount.getValue().intValue());
            this.updateParticles = false;
        }
        this.ps.tick(this.delta.getValue().intValue());
        this.ps.dist = this.radius.getValue().intValue();
        ParticleSystem.SPEED = (float)this.speed.getValue().doubleValue();
    }

    @Override
    public void onRender2D(EventRender2D event) {
        if (mc.currentScreen == Opium.CLICK_GUI) {
            //this.ps.render();
        }
    }
}