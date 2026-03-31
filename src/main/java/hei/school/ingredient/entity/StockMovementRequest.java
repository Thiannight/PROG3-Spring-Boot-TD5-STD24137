package hei.school.ingredient.entity;

public class StockMovementRequest {
    private Unit unit;
    private Double quantity;
    private MovementTypeEnum type;

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public MovementTypeEnum getType() { return type; }
    public void setType(MovementTypeEnum type) { this.type = type; }
}