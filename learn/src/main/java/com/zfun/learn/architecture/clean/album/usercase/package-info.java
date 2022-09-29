/**
 * clean架构中的user case层，依赖于entities，封装通用的业务规则，方便外部使用。<br/>
 * 具有以下优势：<br/>
 * 1，避免代码重复（例如，好多地方直接使用Entity来实现相同的业务逻辑会造成代码重复）。<br/>
 * 2，改善可读性（将相同的业务逻辑收敛到一个类里面了）。<br/>
 * 3，改善可测试性（一个类便于注入fake数据来测试）。<br/>
 * 4，让您能够划分好职责，从而避免出现大型类（在这里便于将复杂业务拆分成小的usercase，然后组合usercase实现复杂功能）。<br/>
 *
 * <P/>
 * 对于简单业务，这层可选，根据自身需求来定，不用死套这些架构的每层都必须存在。<br/>
 * 对于需要多个entities来完成的业务逻辑，还是建议在此层来实现，例如，需要AlbumEntity和MusicEntity来共同完成的业务逻辑。<br/>
 * */
package com.zfun.learn.architecture.clean.album.usercase;