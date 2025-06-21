@import dev.paul.cartlink.product.model.Product
@import java.util.List
@import java.util.Map // For product.specifications

@param Product product
@param List<String> missingFields
@param String suggestionBanner
@param Map<String, Object> model // Or directly access spring:csrfInput if needed and available

<!DOCTYPE html>
<html>
<head>
    <title>Edit Product: ${product.getName()}</title>
    <style>
        body { font-family: sans-serif; margin: 20px; }
        .container { width: 800px; margin: auto; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input[type="text"], input[type="number"], textarea, select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .banner { padding: 10px; background-color: #eef; border: 1px solid #dde; margin-bottom: 20px; border-radius: 4px;}
        .btn { padding: 10px 15px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn:hover { background-color: #0056b3; }
        hr { margin-top: 20px; margin-bottom: 20px; }
        h2, h3 { color: #333; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Edit Product: ${product.getName()}</h2>

        @if (suggestionBanner != null && !suggestionBanner.isEmpty())
            <div class="banner">
                <p>${suggestionBanner}</p>
            </div>
        @endif

        <form action="/merchant/form/${product.getProductId()}" method="post">
            @if(model.containsKey("_csrf"))
                @let csrf = (org.springframework.security.web.csrf.CsrfToken) model.get("_csrf")
                <input type="hidden" name="${csrf.getParameterName()}" value="${csrf.getToken()}"/>
            @endif

            <h3>Product Details</h3>
            <div class="form-group">
                <label for="name">Name:</label>
                <input type="text" id="name" name="name" value="${product.getName()}" readonly>
            </div>
            <div class="form-group">
                <label for="manufacturer">Manufacturer:</label>
                <input type="text" id="manufacturer" name="manufacturer" value="${product.getManufacturer()}" readonly>
            </div>
            <div class="form-group">
                <label for="price">Price:</label>
                <input type="number" id="price" name="price" value="${product.getPrice()}" readonly>
            </div>
            <div class="form-group">
                <label for="description">Description:</label>
                <textarea id="description" name="description" rows="3" readonly>${product.getDescription()}</textarea>
            </div>
            <div class="form-group">
                <label for="type">Product Type:</label>
                <input type="text" id="type" name="type" value="${product.getType()}" readonly>
            </div>

            <hr>
            <h3>Existing Specifications</h3>
            @if (product.getSpecifications() != null && !product.getSpecifications().isEmpty())
                @for (Map.Entry<String, String> spec : product.getSpecifications().entrySet())
                    <div class="form-group">
                        <label for="spec_${spec.getKey()}">${spec.getKey()}:</label>
                        <input type="text" id="spec_${spec.getKey()}" name="specifications[${spec.getKey()}]" value="${spec.getValue()}">
                    </div>
                @endfor
            @else
                <p>No additional specifications currently defined for this product.</p>
            @endif

            @if (missingFields != null && !missingFields.isEmpty())
                <hr>
                <h3>Add Missing Information</h3>
                <p>Help improve this product listing by providing the following details:</p>
                @for (String fieldName : missingFields)
                    <div class="form-group">
                        <label for="spec_new_${fieldName}">${fieldName}:</label>
                        <input type="text" id="spec_new_${fieldName}" name="specifications[${fieldName}]" placeholder="Enter ${fieldName}">
                    </div>
                @endfor
            @endif

            <div class="form-group">
                <button type="submit" class="btn">Save Changes</button>
            </div>
        </form>
    </div>
</body>
</html>
