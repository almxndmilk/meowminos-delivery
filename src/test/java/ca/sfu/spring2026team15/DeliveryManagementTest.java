package ca.sfu.spring2026team15;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Tests for GameLogicHelper.deliveriesNeeded() which requires House objects.
 */
public class DeliveryManagementTest {

    @Test
    public void deliveriesNeededAllUndelivered() {
        List<House> houses = new ArrayList<>();
        houses.add(new House(100f, 100f, null));
        houses.add(new House(200f, 200f, null));
        houses.add(new House(300f, 300f, null));
        assertEquals(3, GameLogicHelper.deliveriesNeeded(houses));
    }

    @Test
    public void deliveriesNeededSomeDelivered() {
        List<House> houses = new ArrayList<>();
        House h1 = new House(100f, 100f, null);
        House h2 = new House(200f, 200f, null);
        House h3 = new House(300f, 300f, null);
        houses.add(h1);
        houses.add(h2);
        houses.add(h3);

        // Deliver to h1: spawn order then deliver
        h1.update(15f);
        h1.tryDeliver(100f, 100f);

        assertEquals(2, GameLogicHelper.deliveriesNeeded(houses));
    }

    @Test
    public void deliveriesNeededAllDelivered() {
        List<House> houses = new ArrayList<>();
        House h1 = new House(100f, 100f, null);
        House h2 = new House(200f, 200f, null);
        houses.add(h1);
        houses.add(h2);

        h1.update(15f);
        h1.tryDeliver(100f, 100f);
        h2.update(15f);
        h2.tryDeliver(200f, 200f);

        assertEquals(0, GameLogicHelper.deliveriesNeeded(houses));
    }

    @Test
    public void deliveriesNeededEmptyList() {
        List<House> houses = new ArrayList<>();
        assertEquals(0, GameLogicHelper.deliveriesNeeded(houses));
    }

    @Test
    public void deliveriesNeededSingleHouseUndelivered() {
        List<House> houses = new ArrayList<>();
        houses.add(new House(500f, 500f, null));
        assertEquals(1, GameLogicHelper.deliveriesNeeded(houses));
    }

    @Test
    public void deliveriesNeededSingleHouseDelivered() {
        List<House> houses = new ArrayList<>();
        House h = new House(500f, 500f, null);
        houses.add(h);
        h.update(15f);
        h.tryDeliver(500f, 500f);
        assertEquals(0, GameLogicHelper.deliveriesNeeded(houses));
    }
}
