<template>
  <span class="badge" :class="type + '-' + value.toLowerCase()">{{ value }}</span>
</template>

<script>
export default {
  props: {
    type: { type: String, required: true },
    value: { type: String, required: true },
  },
}
</script>

<style scoped>
.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.75em;
  font-weight: 600;
  text-transform: uppercase;
}

.severity-critical { background: #fde8e8; color: #c53030; }
.severity-high { background: #feebc8; color: #c05621; }
.severity-medium { background: #fefcbf; color: #975a16; }
.severity-low { background: #e6fffa; color: #276749; }

.category-data_breach { background: #fed7d7; color: #9b2c2c; }
.category-identity_exposure { background: #feebc8; color: #9c4221; }
.category-pii_exposure { background: #e9d8fd; color: #553c9a; }
.category-social_footprint { background: #c6f6d5; color: #276749; }
</style>
