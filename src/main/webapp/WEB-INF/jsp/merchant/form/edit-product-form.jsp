<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Edit Product: <c:out value="${product.name}" /></title>
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
        <h2>Edit Product: <c:out value="${product.name}" /></h2>

        <c:if test="${not empty suggestionBanner}">
            <div class="banner">
                <p><c:out value="${suggestionBanner}" /></p>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/merchant/form/${product.productId}" method="post">
            <h3>Product Details</h3>
            <div class="form-group">
                <label for="name">Name:</label>
                <input type="text" id="name" name="name" value="<c:out value="${product.name}" />" readonly>
            </div>
            <div class="form-group">
                <label for="manufacturer">Manufacturer:</label>
                <input type="text" id="manufacturer" name="manufacturer" value="<c:out value="${product.manufacturer}" />" readonly>
            </div>
            <div class="form-group">
                <label for="price">Price:</label>
                <input type="number" id="price" name="price" value="<c:out value="${product.price}" />" readonly>
            </div>
            <div class="form-group">
                <label for="description">Description:</label>
                <textarea id="description" name="description" rows="3" readonly><c:out value="${product.description}" /></textarea>
            </div>
            <div class="form-group">
                <label for="type">Product Type:</label>
                <input type="text" id="type" name="type" value="<c:out value="${product.type}" />" readonly>
            </div>

            <hr>
            <h3>Existing Specifications</h3>
            <c:choose>
                <c:when test="${not empty product.specifications}">
                    <c:forEach var="spec" items="${product.specifications}">
                        <div class="form-group">
                            <label for="spec_${spec.key}"><c:out value="${spec.key}" />:</label>
                            <input type="text" id="spec_${spec.key}" name="specifications[${spec.key}]" value="<c:out value="${spec.value}" />">
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>No additional specifications currently defined for this product.</p>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty missingFields}">
                <hr>
                <h3>Add Missing Information</h3>
                <p>Help improve this product listing by providing the following details:</p>
                <c:forEach var="fieldName" items="${missingFields}">
                    <div class="form-group">
                        <label for="spec_new_${fieldName}"><c:out value="${fieldName}" />:</label>
                        <input type="text" id="spec_new_${fieldName}" name="specifications[${fieldName}]" placeholder="Enter <c:out value="${fieldName}" />">
                    </div>
                </c:forEach>
            </c:if>

            <div class="form-group">
                <button type="submit" class="btn">Save Changes</button>
            </div>
        </form>
    </div>
</body>
</html>
