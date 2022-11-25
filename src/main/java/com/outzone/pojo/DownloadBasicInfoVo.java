package com.outzone.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadBasicInfoVo {
    Long groupId;
    Long  id;
    Long parentId;
    String filename;
}
