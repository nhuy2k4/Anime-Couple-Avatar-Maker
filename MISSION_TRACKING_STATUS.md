# Hướng dẫn Mission Tracking - Đã tích hợp

## ✅ **Đã hoàn thành tích hợp:**

### 1. **PhotographFragment** - Mission "Save 1 Photo" (50💎)
```kotlin
// File: PhotographFragment.kt
// Vị trí: trong method saveCurrentOutfit()
MissionTracker.Mission.savePhoto(requireContext(), lifecycleScope)
```
**Khi nào được gọi:** Khi user lưu ảnh thành công từ editor

### 2. **CosplayFragment** - Mission "Play 1 Match" (30💎)
```kotlin
// File: CosplayFragment.kt  
// Vị trí: trong method navigateToCategory()
MissionTracker.Mission.playMatch(requireContext(), lifecycleScope)
```
**Khi nào được gọi:** Khi user chọn character để bắt đầu trận cosplay/PK

### 3. **HomeFragment** - Mission "Watch 1 Ad" (20💎)
```kotlin
// File: HomeFragment.kt
// Vị trí: trong method showRewardedAd()
MissionTracker.Mission.watchAd(requireContext(), lifecycleScope)
```
**Khi nào được gọi:** Khi user xem quảng cáo thành công

### 4. **HomeFragment** - Mission "Daily Login" (25💎)
```kotlin
// File: HomeFragment.kt
// Vị trí: trong method checkRewardStatus()
// Tự động được gọi khi mở HomeFragment
rewardRepository.updateMissionProgress(playerId, "LOGIN")
```
**Khi nào được gọi:** Tự động khi user mở HomeFragment

## ❌ **Còn thiếu - cần bạn tự thêm:**

### 5. **Mission "Win 1 Match" (80💎)** - Chưa tích hợp
**Cần thêm vào:** Fragment/Activity xử lý kết quả trận đấu PK
```kotlin
// Thêm vào nơi xử lý kết quả thắng cuộc
MissionTracker.Mission.winMatch(requireContext(), lifecycleScope)
```

**Các vị trí có thể cần thêm:**
- ArenaSelectFragment (nếu có)
- PK result screen/dialog
- Match result handler
- Cosplay competition result

## 🔧 **Cách sử dụng MissionTracker:**

### Cách 1: Sử dụng helper methods
```kotlin
MissionTracker.Mission.savePhoto(context, lifecycleScope)
MissionTracker.Mission.playMatch(context, lifecycleScope)
MissionTracker.Mission.winMatch(context, lifecycleScope)
MissionTracker.Mission.watchAd(context, lifecycleScope)
MissionTracker.Mission.login(context, lifecycleScope)
```

### Cách 2: Sử dụng method tổng quát
```kotlin
MissionTracker.updateProgress(
    context = requireContext(),
    missionType = MissionTracker.Type.WIN_MATCH,
    amount = 1,
    scope = lifecycleScope
)
```

## 📍 **Tìm nơi cần thêm "Win Match":**

1. **Tìm fragment/activity xử lý PK result:**
```bash
# Tìm các file có liên quan đến result, win, match
grep -r "win\|result\|victory" app/src/main/java/
```

2. **Hoặc tìm trong navigation:**
```bash
# Tìm trong navigation graph
grep -r "arena\|pk\|battle" app/src/main/res/navigation/
```

3. **Kiểm tra các dialog xử lý kết quả:**
```kotlin
// Trong dialog hiển thị kết quả trận đấu
if (playerWon) {
    MissionTracker.Mission.winMatch(requireContext(), lifecycleScope)
}
```

## 🎯 **Test mission system:**

1. **Save Photo:** Vào editor → chỉnh sửa → bấm Save → kiểm tra task progress
2. **Play Match:** Vào Cosplay → chọn character → kiểm tra task progress  
3. **Watch Ad:** Vào Task → bấm "Go Now" cho Watch Ad → kiểm tra progress
4. **Daily Login:** Mở app → tự động complete
5. **Win Match:** Chưa test được - cần tích hợp thêm

## 🔄 **Luồng hoạt động hiện tại:**

1. User mở HomeFragment → Auto login mission
2. Click btnDaily → Mở DailyRewardDialog → Claim daily rewards
3. Click btnTask → Mở TaskDialog → Xem missions + click "Go Now"
4. Làm missions → MissionTracker update progress → UI refresh
5. Quay lại TaskDialog → Claim completed missions

## 📝 **Notes:**
- Tất cả mission tracking đã hoạt động trừ "Win Match"
- Player ID hiện tại đang hardcode = 1, có thể cần thay đổi
- UI sẽ tự động refresh khi có mission complete
- Database sẽ tự tạo missions mới mỗi ngày
