@param String typeId
@param String productType
@param java.util.List fields

<!DOCTYPE html>
<html>
<head>
    <title>Create Product: ${productType}</title>
    <style>
        body { font-family: sans-serif; margin: 20px; }
        .container { width: 600px; margin: auto; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input, select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .btn { padding: 10px 15px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn:hover { background-color: #0056b3; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Create Product: ${productType}</h2>
        <form action="/api/v1/merchants/products" method="post">
            <input type="hidden" name="typeId" value="${typeId}" />
            <input type="hidden" name="productType" value="${productType}" />
            @for(field : fields)
                <div class="form-group">
                    <label for="${field.name}">${field.label}:</label>
                    @if(field.type == "Integer" || field.type == "Decimal")
                        <input type="number" id="${field.name}" name="${field.name}" step="any" required />
                    @else
                        <input type="text" id="${field.name}" name="${field.name}" required />
                    @endif
                </div>
            @endfor
            <div class="form-group">
                <button type="submit" class="btn">Create Product</button>
            </div>
        </form>
    </div>
</body>
</html> 