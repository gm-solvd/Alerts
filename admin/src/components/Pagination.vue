<template>
  <div v-if="totalPages > 1" class="pagination">
    <button :disabled="page <= 0" @click="$emit('change', page - 1)">Prev</button>
    <span>Page {{ page + 1 }} of {{ totalPages }}</span>
    <button :disabled="page >= totalPages - 1" @click="$emit('change', page + 1)">Next</button>
  </div>
</template>

<script>
export default {
  props: {
    page: { type: Number, required: true },
    totalPages: { type: Number, required: true },
  },
  emits: ['change'],
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.pagination button {
  padding: 6px 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 0.9em;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination button:not(:disabled):hover {
  background: #f0f0f0;
}

.pagination span {
  font-size: 0.9em;
  color: #666;
}
</style>
