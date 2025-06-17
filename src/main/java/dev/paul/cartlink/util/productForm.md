
# 🛍️ Dynamic Product Template System using LLM + Evolving Schemas


This document outlines the architecture and process for handling product creation dynamically using a hybrid of:
- Basic structured fields,
- LLM-generated attribute suggestions,
- Document-based product storage (per productType),
- Evolving field templates over time.

---

## 📂 1. Supported Main Categories

We currently support the following high-level product categories:

- 📱 **Electronics**
- 👗 **Apparel**
- 💄 **Cosmetics**
- ⚗️ **Chemicals**
- 🥤 **Beverages**
- 🛠️ **Mechanical Machine**
- ⚙️ **Electromechanical Machine**

Each product is tagged with a `category` and `productType` within these.

---

## 🧱 2. Core Product Structure (Base Fields)

All products share a core structure stored in a relational store:

```json
{
  "category": "electronics",
  "productType": "smartphone",
  "brand": "Samsung",
  "name": "Galaxy S22",
  "description": "High-end smartphone with AMOLED display"
}
````

This minimal structure is used to:

* Route storage
* Inform the LLM
* Build dynamic product forms

---

## 🤖 3. LLM-Based Field Suggestion

Upon creating a product, an LLM is used to generate relevant fields **based on**:

* The `category`
* The `productType`
* The `description` or name

### Example Prompt

```text
Generate relevant product attributes for a productType "smartphone" in the "electronics" category.
Respond in JSON with suggested field names, types, and descriptions.
```

### Example LLM Output

```json
{
  "fields": [
    { "name": "screenSizeInches", "type": "Decimal", "label": "Screen Size (inches)" },
    { "name": "batteryCapacityMah", "type": "Integer", "label": "Battery (mAh)" },
    { "name": "storageGb", "type": "Integer", "label": "Storage (GB)" },
    { "name": "ramGb", "type": "Integer", "label": "RAM (GB)" }
  ]
}
```

---

## 📄 4. Document Storage by ProductType

Each `productType` has a dedicated **document collection** (or table if using JSONB):

* `products_smartphone`
* `products_hoodie`
* `products_skirt`
* etc.

Each product is saved with dynamic fields in a flexible schema.

---

## 🧠 5. Template Schema per ProductType

Each `productType` maintains a **template schema**:

```json
{
  "productType": "smartphone",
  "fields": [
    "screenSizeInches",
    "batteryCapacityMah",
    "storageGb",
    "ramGb"
  ],
  "source": "LLM",
  "status": "active",
  "usageStats": {
    "screenSizeInches": 128,
    "batteryCapacityMah": 120
  }
}
```

This schema is:

* Used to render dynamic forms when creating or editing products
* Updated passively based on user input

* When a `productType` is first introduced, the LLM-generated field suggestions form the base schema.
* Templates are stored and reused to avoid regenerating for each product.

---
### 6. Buyer-Informed Template Evolution ✅

Rather than asking sellers to define missing fields:

* After a **product view or purchase**, buyers are prompted with:

  > “Was any important information missing?”

* Responses are logged as structured suggestions:

```json
{
  "productId": "prd_882391",
  "buyerId": "user_1201",
  "missingFieldsSuggested": ["cameraResolution", "durabilityRating"]
}
```

* Suggestions are:

  * **Aggregated** per productType
  * **Promoted** to schema if they appear frequently
  * Optionally used to refine the LLM prompt for better next-time suggestions

---

## 🧩 7. Missing Fields Flow

When a user views or edits a product with missing template fields:

* The server can render a suggestion banner: “Add missing fields to help improve the product listing.”
* Users can optionally supply values
* These values feed into the `usageStats` for schema confidence boosting

---

## 🌱 8. Summary of Evolving Workflow

1. User submits basic product (category, productType, brand, name, description)
2. LLM suggests dynamic fields
3. Fields rendered in dynamic form for submission
4. Product is stored in per-type document collection
5. Template schema is used and evolved over time
6. Avoids overwhelming users with field management
These fields are used to generate a form for the user to fill.

---



## 🔌 API Endpoints

| Endpoint                       | Description                               |
| ------------------------------ | ----------------------------------------- |
| `GET /categories` | Returns list of available productTypes    |
| `GET /form/:productType`       | Returns server-rendered HTML form         |
| `POST /product`                | Saves product and its fields              |
| `POST /feedback/:productId`    | Accepts buyer feedback for missing fields |

---

## 🚀 Benefits of the Approach

* ✅ Simple base model keeps input lightweight
* 🤖 LLM-powered suggestions make forms intelligent
* 🔁 Schema evolves based on **real buyer behavior**
* 🧠 
* 📚 Uses document storage to accommodate flexible schemas





## ✅ **Simplified Combined Review & Schema Feedback (Anonymous)**

### 🔁 When to Trigger:

* Right after the order is **completed** (funds disbursed to seller).
* Send a **single email** with a link to a **short, anonymous review form**.

---

### ✉️ Email Template (1 CTA)

**Subject:** *“Quick feedback on your recent order?”*
**Body:**

> Hi! Thanks for purchasing **\[Product Name]**.
>
> We’d appreciate 30 seconds of your time to help improve product listings and seller quality.
>
> [Leave Anonymous Feedback](https://yourapp.com/feedback?productId=XXX)
>
> No login required. Just click and submit!

---

### 📝 Anonymous Feedback Form

**Fields:**

1. **How was the product?**

   * ⭐ Rating (1–5)
   * 📝 Optional comment

2. **How was the seller experience?**

   * ⭐ Rating (1–5)
   * 📝 Optional comment

3. **Was any product info missing or unclear?**

   * 🔘 Yes / No
   * If “Yes”:

     * [ ] Suggested missing info (checkboxes)
     * 📝 Free-text suggestion field

**Hidden inputs:**

* `productId`, `productType`, etc.

---

### 🧩 How It Works Internally

* No user ID is required (anonymous).
* Each form submission creates two lightweight entries:

  * A `SellerReview` or `ProductReview` with flag `isAnonymous: true`.
  * A `SchemaFeedback` entry to update product type metadata.

---

### 🧠 Updating the Template Schema

Every few days:

1. Aggregate new `SchemaFeedback` suggestions by `productType`.
2. Rank by frequency.
3. Update the JSON schema used by your form-renderer in the backend.







